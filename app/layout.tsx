import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Great_Vibes } from "next/font/google";
import Link from 'next/link';
import Image from 'next/image';
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const greatVibes = Great_Vibes({
  weight: '400',
  subsets: ["latin"],
  variable: "--font-great-vibes",
});

export const metadata: Metadata = {
  title: "Dasurv",
  description: "Book your wellness journey",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} ${greatVibes.variable} antialiased`}
      >
        <nav className="fixed w-full top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center">
                <Link href="/" className="relative hover:opacity-80 transition-opacity">
                  <Image
                    src="/icons/brand.png"
                    alt="Dasurv"
                    width={150}
                    height={60}
                    priority
                    style={{ transform: "rotate(0.5deg)" }}
                  />
                </Link>
              </div>
              <div className="hidden sm:flex sm:space-x-1">
                <Link
                  href="/"
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-all"
                >
                  Home
                </Link>
                <Link
                  href="/book"
                  className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50 rounded-md transition-all"
                >
                  Book Now
                </Link>
               
              </div>
            </div>
          </div>
        </nav>
        <main className="pt-24">
          {children}
        </main>
      </body>
    </html>
  );
}
