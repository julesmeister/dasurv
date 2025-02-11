export interface Reservation {
  id: string;
  customerName: string;
  email: string;
  phone: string;
  date: Date;
  time: string;
  service: string;
  status: 'pending' | 'confirmed' | 'cancelled';
  notes?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface Service {
  id: string;
  name: string;
  description: string;
  duration: number; // in minutes
  price: number;
  available: boolean;
}

export interface AdminUser {
  uid: string;
  email: string;
  role: 'admin';
}
