import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StudyGroupListComponent } from './pages/study-group-list/study-group-list.component';
import { StudyGroupFormComponent } from './pages/study-group-form/study-group-form.component';
import { StudyGroupDetailsComponent } from './pages/study-group-details/study-group-details.component';
import { StudyGroupCalendarComponent } from './pages/study-group-calendar/study-group-calendar.component';
import { StudyGroupStatsComponent } from './pages/study-group-stats/study-group-stats.component';
import {StudyGroupAuditComponent} from "./pages/study-group-audit/study-group-audit.component";

const routes: Routes = [
  { path: '',         component: StudyGroupListComponent },
  { path: 'new',      component: StudyGroupFormComponent },
  { path: 'calendar', component: StudyGroupCalendarComponent },
  { path: 'stats',    component: StudyGroupStatsComponent },   // ✅ AVANT :id
  { path: 'edit/:id', component: StudyGroupFormComponent },
  { path: ':id',      component: StudyGroupDetailsComponent },
  { path: ':id/audit', component: StudyGroupAuditComponent }, // ✅ avant :id
// ← toujours en dernier
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudyGroupsRoutingModule {}
