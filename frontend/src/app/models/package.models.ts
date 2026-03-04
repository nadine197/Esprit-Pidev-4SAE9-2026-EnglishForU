export type PackageType = 'COURSE_ONLY' | 'EVENT_ONLY' | 'MIXED';

export interface PackageItemResponse {
  id: number;
  itemType: string;   // if you have an enum later, replace with union type
  itemId: number;
}

export interface PackageOfferResponse {
  id: number;
  name: string;
  description: string;
  type: PackageType;
  durationDays: number;
  price: number;          // backend BigDecimal -> number in TS
  isActive: boolean;
  createdAt: string;      // backend Instant -> ISO string
  items: PackageItemResponse[];
}

// Request DTOs (guessing minimal shape based on controller usage)
export interface CreatePackageOfferRequest {
  name: string;
  description?: string;
  type: PackageType;
  durationDays: number;
  price: number;
  isActive?: boolean; // if backend supports it
}

export interface AddPackageItemRequest {
  itemType: string;
  itemId: number;
}