export interface CreatePaymentRequest {
  studentId: number;
  targetType: TargetType;
  targetId: number;
  amountOriginal: number;
  discountAmount?: number;
  paymentMethod: PaymentMethod;
}
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export type PaymentMethod = 'CASH' | 'STRIPE' | 'FLOUCI';
export type TargetType = 'PACKAGE'| 'EVENT'; // add more later
export interface ConfirmPaymentRequest {
  provider: string;
  providerRef: string;
}


export interface PaymentResponse {
  id: number;
  studentId: number;
  targetType: TargetType;
  targetId: number;

  amountOriginal: number;
  discountAmount: number;
  amountFinal: number;

  provider: string | null;     // backend String (can be null)
  providerRef: string | null;  // backend String (can be null)

  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  createdAt: string; // Instant -> ISO string
}