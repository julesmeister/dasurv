// /Users/julesmeister/Documents/GitHub/dasurv/app/models/inventory.ts

// /Users/julesmeister/Documents/GitHub/dasurv/app/models/inventory.ts

export interface InventoryItem {
  id: number;
  name: string;
  current: number;
  minimum: number;
  category: string; // New field
  supplier: string; // New field
  cost: number; // New field
  price: number; // New field
  expirationDate?: Date; // New field, optional
  reorderLevel: number; // New field
  imageUrl?: string; // New field, optional
}

// Optional: Add functions to manage inventory items
export const fetchInventoryItems = async (): Promise<InventoryItem[]> => {
  // Implement fetching logic from Firestore or any other source
  return [];
};