export interface Service {
  id?: string; // Using string for Firestore document IDs
  name: string;
  icon?: string;
  description?: string;
  duration: string;
  price: string;
  status: 'active' | 'inactive';
}

// Initial services data for seeding the database
export const initialServices: Omit<Service,  'price' | 'status'>[] = [
  { 
    id: '1', // Placeholder ID
    name: 'Swedish Massage',
    icon: '/icons/swedish.svg',
    description: 'Gentle, relaxing massage using long strokes and kneading. Perfect for first-time clients and those seeking stress relief.',
    duration: '60 mins'
  },
  { 
    id: '2', // Placeholder ID
    name: 'Deep Tissue Massage',
    icon: '/icons/deep-tissue.svg',
    description: 'Deep tissue massage targeting deeper layers of muscle and connective tissue. Ideal for chronic pain and tension relief.',
    duration: '60 mins'
  },
  { 
    id: '3', // Placeholder ID
    name: 'Sports Massage',
    icon: '/icons/sports.svg',
    description: 'Focused on muscle recovery and injury prevention. Great for athletes and active individuals.',
    duration: '60 mins'
  },
  { 
    id: '4', // Placeholder ID
    name: 'Therapeutic Massage',
    icon: '/icons/therapeutic.svg',
    description: 'Customized massage targeting specific areas of concern. Tailored to your unique needs and preferences.',
    duration: '60 mins'
  }
];
