import { AdminAuth } from '@/app/components/auth/AdminAuth';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AdminAuth>
      <div className="min-h-screen bg-gray-100">
        <nav className="bg-white shadow-sm">
          {/* Add admin navigation here */}
        </nav>
        <main className="container mx-auto px-4 py-8">
          {children}
        </main>
      </div>
    </AdminAuth>
  );
}
