export interface CreatePaymentRequest {
  studentId: number;
  targetType: TargetType;
  targetId: number;
  amountOriginal: number;
  discountAmount?: number;
  paymentMethod: PaymentMethod;
}
export type PaymentMethod = 'CASH' | 'STRIPE' | 'FLOUCI';
export type TargetType = 'PACKAGE'| 'EVENT'; // add more later
export interface ConfirmPaymentRequest {
  provider: string;
  providerRef: string;
}

export interface PaymentResponse {
  id: number;
  status: string;     // can be union if you have enum
  amount: number;
  createdAt: string;
  provider?: string;
  providerRef?: string;
  promoCodeId?: number | null;
}