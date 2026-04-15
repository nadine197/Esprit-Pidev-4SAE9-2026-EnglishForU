from __future__ import annotations

import json
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import joblib
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import ExtraTreesClassifier, RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import ConfusionMatrixDisplay, confusion_matrix
from sklearn.model_selection import StratifiedKFold, cross_validate, train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

BASE_DIR = Path(__file__).resolve().parent
DATASET_DIR = BASE_DIR / "dataset"
OUTPUT_DIR = BASE_DIR / "output"
TARGET_COLUMN = "Engagement_Level"
DROP_COLUMNS = {"Student_ID"}
RANDOM_STATE = 42

def main() -> None:
    dataset_paths = sorted(DATASET_DIR.glob("*.csv"))
    if not dataset_paths:
        raise FileNotFoundError(f"No CSV datasets found in {DATASET_DIR}")

    if OUTPUT_DIR.exists():
        shutil.rmtree(OUTPUT_DIR)
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    for dataset_path in dataset_paths:
        train_for_dataset(dataset_path)

    print("Training completed.")

def train_for_dataset(dataset_path: Path) -> None:
    df = pd.read_csv(dataset_path)

    if TARGET_COLUMN not in df.columns:
        print(f"Skipped {dataset_path.name}: does not contain target column '{TARGET_COLUMN}'")
        return

    dataset_name = dataset_path.stem
    dataset_output_dir = OUTPUT_DIR / dataset_name
    models_dir = dataset_output_dir / "models"
    
    working_df = df.drop(columns=[column for column in DROP_COLUMNS if column in df.columns]).copy()
    X = working_df.drop(columns=[TARGET_COLUMN])
    y = working_df[TARGET_COLUMN].astype(str)
    
    if y.nunique() < 2:
        print(f"Skipped {dataset_name}: The target column contains only one class.")
        return

    minimum_class_count = int(y.value_counts().min())
    if minimum_class_count < 2:
        print(f"Skipped {dataset_name}: At least one engagement class has fewer than two rows.")
        return

    models_dir.mkdir(parents=True, exist_ok=True)

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.25,
        random_state=RANDOM_STATE,
        stratify=y,
    )

    numeric_columns = X.select_dtypes(include=["number"]).columns.tolist()
    categorical_columns = [column for column in X.columns if column not in numeric_columns]

    preprocessor = build_preprocessor(numeric_columns, categorical_columns)
    model_candidates = build_model_candidates(preprocessor)
    cv_results = evaluate_candidates(model_candidates, X_train, y_train)

    best_model_name = cv_results.iloc[0]["model_name"]
    best_model = model_candidates[best_model_name]
    best_model.fit(X_train, y_train)

    statistics_dir = dataset_output_dir / "statistics"
    statistics_dir.mkdir(parents=True, exist_ok=True)
    export_engagement_png_charts(
        statistics_dir=statistics_dir,
        y_full=y,
        cv_results=cv_results,
        y_test=y_test,
        y_pred_test=best_model.predict(X_test),
    )

    joblib.dump(best_model, models_dir / "engagement_model.joblib")
    with (models_dir / "model_metadata.json").open("w", encoding="utf-8") as file:
        json.dump(
            {
                "dataset_name": dataset_name,
                "target_column": TARGET_COLUMN,
                "best_model_name": best_model_name,
                "feature_columns": X.columns.tolist(),
                "numeric_columns": numeric_columns,
                "categorical_columns": categorical_columns,
                "classes": sorted(y.unique()),
            },
            file,
            indent=2,
        )

    print(f"Successfully trained '{dataset_name}' using '{best_model_name}'.")


def export_engagement_png_charts(
    statistics_dir: Path,
    y_full: pd.Series,
    cv_results: pd.DataFrame,
    y_test: pd.Series,
    y_pred_test: np.ndarray,
) -> None:
    """User-facing PNG summaries for the admin UI (engagement / dropout-style narratives)."""

    fig, axis = plt.subplots(figsize=(8, 5))
    counts = y_full.value_counts().sort_index()
    counts.plot(kind="bar", ax=axis, color="#6366f1", edgecolor="white")
    axis.set_title("How learners are spread across engagement levels")
    axis.set_xlabel("Engagement level (proxy for staying on track)")
    axis.set_ylabel("Number of learners in this dataset")
    axis.tick_params(axis="x", rotation=25)
    fig.tight_layout()
    fig.savefig(statistics_dir / "01_engagement_mix.png", dpi=120, bbox_inches="tight")
    plt.close(fig)

    fig, axis = plt.subplots(figsize=(8, 5))
    chart = cv_results.sort_values("cv_macro_f1_mean")
    axis.barh(chart["model_name"], chart["cv_macro_f1_mean"], color="#8b5cf6")
    axis.set_xlabel("Cross-validation macro F1 (higher is usually better)")
    axis.set_title("Model comparison on training folds")
    fig.tight_layout()
    fig.savefig(statistics_dir / "02_model_comparison.png", dpi=120, bbox_inches="tight")
    plt.close(fig)

    labels = sorted(y_full.unique())
    matrix = confusion_matrix(y_test, y_pred_test, labels=labels)
    fig, axis = plt.subplots(figsize=(7, 6))
    display = ConfusionMatrixDisplay(confusion_matrix=matrix, display_labels=labels)
    display.plot(ax=axis, cmap="Blues", colorbar=False)
    axis.set_title("Test set: predicted vs actual engagement")
    fig.tight_layout()
    fig.savefig(statistics_dir / "03_test_confusion_matrix.png", dpi=120, bbox_inches="tight")
    plt.close(fig)


def build_preprocessor(
    numeric_columns: list[str], categorical_columns: list[str]
) -> ColumnTransformer:
    numeric_transformer = Pipeline(
        steps=[("imputer", SimpleImputer(strategy="median"))]
    )
    categorical_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="most_frequent")),
            ("encoder", OneHotEncoder(handle_unknown="ignore")),
        ]
    )

    return ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, numeric_columns),
            ("cat", categorical_transformer, categorical_columns),
        ]
    )

def build_model_candidates(preprocessor: ColumnTransformer) -> dict[str, Pipeline]:
    return {
        "random_forest": Pipeline(
            steps=[
                ("preprocessor", preprocessor),
                (
                    "classifier",
                    RandomForestClassifier(
                        n_estimators=250,
                        random_state=RANDOM_STATE,
                        class_weight="balanced",
                    ),
                ),
            ]
        ),
        "extra_trees": Pipeline(
            steps=[
                ("preprocessor", preprocessor),
                (
                    "classifier",
                    ExtraTreesClassifier(
                        n_estimators=300,
                        random_state=RANDOM_STATE,
                        class_weight="balanced",
                    ),
                ),
            ]
        ),
        "logistic_regression": Pipeline(
            steps=[
                ("preprocessor", preprocessor),
                (
                    "classifier",
                    LogisticRegression(
                        max_iter=4000,
                        class_weight="balanced",
                    ),
                ),
            ]
        ),
    }

def evaluate_candidates(
    candidates: dict[str, Pipeline], X_train: pd.DataFrame, y_train: pd.Series
) -> pd.DataFrame:
    minimum_class_count = int(y_train.value_counts().min())
    split_count = max(2, min(5, minimum_class_count))
    cross_validator = StratifiedKFold(
        n_splits=split_count, shuffle=True, random_state=RANDOM_STATE
    )
    rows: list[dict[str, Any]] = []

    for model_name, model in candidates.items():
        scores = cross_validate(
            model,
            X_train,
            y_train,
            cv=cross_validator,
            scoring=["accuracy", "f1_macro", "f1_weighted"],
            n_jobs=1,
            return_train_score=False,
        )

        rows.append(
            {
                "model_name": model_name,
                "cv_accuracy_mean": round(np.mean(scores["test_accuracy"]), 4),
                "cv_accuracy_std": round(np.std(scores["test_accuracy"]), 4),
                "cv_macro_f1_mean": round(np.mean(scores["test_f1_macro"]), 4),
                "cv_macro_f1_std": round(np.std(scores["test_f1_macro"]), 4),
                "cv_weighted_f1_mean": round(np.mean(scores["test_f1_weighted"]), 4),
                "cv_weighted_f1_std": round(np.std(scores["test_f1_weighted"]), 4),
            }
        )

    return pd.DataFrame(rows).sort_values(
        by=["cv_macro_f1_mean", "cv_accuracy_mean"], ascending=False
    )

if __name__ == "__main__":
    main()
