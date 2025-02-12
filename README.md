# Dasurv - Spa Management System

A comprehensive spa management system built with Next.js 13+, featuring both client-facing booking interfaces and powerful admin tools for efficient spa operations management.

## Features

### For Clients
- Easy-to-use spa service booking interface
- Real-time availability checking
- Service catalog with detailed descriptions
- Booking history and status tracking
- Appointment reminders and notifications

### For Administrators
- **Inventory Management**
  - Track spa supplies (oils, towels, etc.)
  - Low stock alerts
  - Usage tracking and forecasting
  - Supplier management

- **Booking Management**
  - View and manage all appointments
  - Real-time calendar updates
  - Staff scheduling and assignment
  - Service capacity management

- **Staff Management**
  - Therapist schedules and availability
  - Performance tracking
  - Service specialization mapping

- **Service Management**
  - Configure service offerings
  - Set pricing and duration
  - Special packages and promotions
  - Service availability settings

## Tech Stack

- **Frontend**: Next.js 13+, React, Tailwind CSS
- **Backend**: Next.js API Routes
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth Google Provider
- **Hosting**: Cloudflare Pages

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/julesmeister/dasurv.git
```

2. Install dependencies:
```bash
npm install
# or
yarn install
```

3. Set up environment variables:
Create a `.env.local` file with the following variables:
```env
NEXT_PUBLIC_FIREBASE_API_KEY=your_api_key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=your_auth_domain
NEXT_PUBLIC_FIREBASE_PROJECT_ID=your_project_id
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=your_storage_bucket
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
NEXT_PUBLIC_FIREBASE_APP_ID=your_app_id
NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=your_measurement_id
```

4. Run the development server:
```bash
npm run dev
# or
yarn dev
```

5. Open [http://localhost:3000](http://localhost:3000) in your browser.

## Project Structure

```
dasurv/
├── app/                    # Next.js 13+ app directory
│   ├── admin/             # Admin dashboard and features
│   ├── book/              # Booking interface
│   ├── api/               # API routes
│   └── lib/               # Shared utilities and configs
├── components/            # Reusable React components
├── public/               # Static files
└── styles/               # Global styles
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
