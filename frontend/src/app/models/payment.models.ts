export interface CreatePaymentRequest {
  studentEmail: string;     // ✅ instead of studentId
  targetId: number;
  amountOriginal: number;
  discountAmount?: number;
  paymentMethod: PaymentMethod;
  idPromoCode?: string | null; // optional, if you want to link payment to promo
}
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export type PaymentMethod = 'CASH' | 'STRIPE' | 'FLOUCI';
export interface ConfirmPaymentRequest {
  provider: string;
  providerRef: string;
}


export interface PaymentResponse {
  id: number;
  studentId: number;
  targetName: string;
  targetId: number;
  studentFullName?: string;
  amountOriginal: number;
  discountAmount: number;
  amountFinal: number;

  provider: string | null;     // backend String (can be null)
  providerRef: string | null;  // backend String (can be null)

  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  createdAt: string; // Instant -> ISO string
}