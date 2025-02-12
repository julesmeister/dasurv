export interface Booking {
  id?: string; // Optional ID for Firestore document IDs
  serviceId: string; // Required ID of the service being booked
  customerName: string; // Required name of the customer
  email: string; // Required email of the customer
  phone: string; // Required phone number of the customer
  date: string; // Required date of the booking
  time: string; // Required time of the booking
  duration: string; // Required duration of the service
  notes: string; // Optional notes from the customer
  status: 'confirmed' | 'pending' | 'canceled'; // Required status of the booking
  createdAt: Date; // Required timestamp for creation
  updatedAt: Date; // Required timestamp for last update
}

// Initial bookings data for seeding the database
export const initialBookings: Omit<Booking, 'status' | 'createdAt' | 'updatedAt'>[] = [
  { 
    id: '1', // Placeholder ID
    serviceId: '1', // Example service ID
    customerName: 'John Doe',
    email: 'john@example.com',
    phone: '123-456-7890',
    date: '2025-02-15',
    time: '10:00',
    duration: '60 mins',
    notes: ''
  },
  { 
    id: '2', // Placeholder ID
    serviceId: '2', // Example service ID
    customerName: 'Jane Smith',
    email: 'jane@example.com',
    phone: '987-654-3210',
    date: '2025-02-16',
    time: '11:00',
    duration: '30 mins',
    notes: ''
  }
];
