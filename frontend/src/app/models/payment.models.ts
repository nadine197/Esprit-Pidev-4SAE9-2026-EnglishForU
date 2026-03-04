export interface CreatePaymentRequest {
  studentId: number;
  packageOfferId: number;
  amount: number;
  promoCodeId?: number | null;
  provider: string;
}

export interface ConfirmPaymentRequest {
  provider: string;
  promoCodeId?: number | null;
  providerRef: string;
  responsePayload?: string | null;
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