/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export', // Required for static site generation
  images: {
    unoptimized: true, // Required for static site generation
    domains: ['firebasestorage.googleapis.com'], // Add your image domains here
  }
}

module.exports = nextConfig
