export type DiscountType = 'PERCENTAGE' | 'FIXED';

export interface PromoCode {
  id: number;
  code: string;
  discountType: DiscountType;
  discountValue: number;
  startDate?: string | null;   // if backend uses LocalDate/Instant -> string
  endDate?: string | null;
  usageLimit?: number | null;
  currentUses?: number | null;
  active: boolean;
  createdAt?: string;          // if exists in backend
}

export interface CreatePromoCodeRequest {
  code: string;
  discountType: DiscountType;
  discountValue: number;
  startDate?: string | null;
  endDate?: string | null;
  usageLimit?: number | null;
  active?: boolean;
}

export interface ApplyPromoRequest {
  code: string;
  studentId: number;
  amountOriginal: number;
}

export interface ApplyPromoResponse {
  valid: boolean;
  promoCode: string;
  discountAmount: number;
  amountFinal: number;
}