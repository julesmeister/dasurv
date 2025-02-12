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
export const initialServices: Omit<Service, 'id' | 'price' | 'status'>[] = [
  { 
    name: 'Swedish Massage',
    icon: '/icons/swedish.svg',
    description: 'Gentle, relaxing massage using long strokes and kneading. Perfect for first-time clients and those seeking stress relief.',
    duration: '60 mins'
  },
  { 
    name: 'Deep Tissue Massage',
    icon: '/icons/deep-tissue.svg',
    description: 'Targets deep muscle layers to release chronic tension. Ideal for those with specific muscle problems or chronic pain.',
    duration: '60 mins'
  },
  { 
    name: 'Sports Massage',
    icon: '/icons/sports.svg',
    description: 'Focused on muscle recovery and injury prevention. Great for athletes and active individuals.',
    duration: '60 mins'
  },
  { 
    name: 'Therapeutic Massage',
    icon: '/icons/therapeutic.svg',
    description: 'Customized massage targeting specific areas of concern. Tailored to your unique needs and preferences.',
    duration: '60 mins'
  }
];
