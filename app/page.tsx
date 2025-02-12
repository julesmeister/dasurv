'use client';

import Link from 'next/link';
import Image from 'next/image';
import { ArrowRightIcon } from '@heroicons/react/24/solid';

const ServicesButton = () => {
  const scrollToServices = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    const element = document.getElementById('services');
    if (element) {
      element.scrollIntoView({ 
        behavior: 'smooth',
        block: 'start'
      });
    }
  };

  return (
    <Link 
      href="#services"
      onClick={scrollToServices}
      className="rounded-md bg-gray-900 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-gray-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-600"
    >
      View Services <ArrowRightIcon className="ml-2 inline-block h-4 w-4" />
    </Link>
  );
};

export default function Home() {
  return (
    <div className="bg-white">
      <div className="relative isolate px-6 pt-14 lg:px-8">
        <div 
          className="absolute inset-x-0 -top-40 -z-10 transform-gpu overflow-hidden blur-3xl sm:-top-80" 
          aria-hidden="true"
        >
          <div 
            className="relative left-[calc(50%-11rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-gradient-to-tr from-[#ff80b5] to-[#9089fc] opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]"
            style={{
              clipPath: 'polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)'
            }}
          />
        </div>
        <div className="mx-auto max-w-2xl lg:mt-10 lg:mb-56 mb-32 sm:mb-48">
          <div className="text-center">
            <div className="mb-8 flex justify-center">
              <Image
                src="/icons/brand.png"
                alt="Dasurv"
                width={400}
                height={160}
                priority
                style={{ transform: "rotate(0deg)" }}
              />
            </div>
            <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-6xl">
              Experience Ultimate Relaxation
            </h1>
            <p className="mt-6 text-lg leading-8 text-gray-600">
              Discover professional massage therapy tailored to your needs. From Swedish relaxation to deep tissue therapy, our expert therapists are dedicated to your well-being and comfort.
            </p>
            <div className="mt-10 flex items-center justify-center gap-x-6">
              <Link 
                href="/book" 
                className="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 flex items-center"
              >
                Book Now
                <ArrowRightIcon className="ml-2 h-5 w-5" />
              </Link>
              <ServicesButton />
            </div>
          </div>
        </div>
        <div 
          className="absolute inset-x-0 top-[calc(100%-13rem)] -z-10 transform-gpu overflow-hidden blur-3xl sm:top-[calc(100%-30rem)]" 
          aria-hidden="true"
        >
          <div 
            className="relative left-[calc(50%+3rem)] aspect-[1155/678] w-[36.125rem] -translate-x-1/2 bg-gradient-to-tr from-[#ff80b5] to-[#9089fc] opacity-30 sm:left-[calc(50%+36rem)] sm:w-[72.1875rem]"
            style={{
              clipPath: 'polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)'
            }}
          />
        </div>
      </div>

      {/* Features Section */}
      <div id="services" className="bg-gray-50 py-24 sm:py-32 scroll-mt-30">
        <div className="mx-auto max-w-7xl px-6 lg:px-8">
          <div className="mx-auto max-w-2xl lg:text-center">
            <h2 className="text-base font-semibold leading-7 text-indigo-600">Our Services</h2>
            <p className="mt-2 text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl">
              Professional Massage Therapy
            </p>
            <p className="mt-6 text-lg leading-8 text-gray-600">
              Choose from our range of specialized massage treatments, each designed to provide specific benefits for your well-being.
            </p>
          </div>
          <div className="mx-auto mt-16 max-w-2xl sm:mt-20 lg:mt-24 lg:max-w-4xl">
            <dl className="grid max-w-xl grid-cols-1 gap-x-8 gap-y-10 lg:max-w-none lg:grid-cols-2 lg:gap-y-16">
              {[
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
                  duration: ' '
                },
                { 
                  name: 'Therapeutic Massage',
                  icon: '/icons/therapeutic.svg',
                  description: 'Customized massage targeting specific areas of concern. Tailored to your unique needs and preferences.',
                  duration: '60 mins'
                }
              ].map((service) => (
                <div key={service.name} className="relative pl-16">
                  <dt className="text-base font-semibold leading-7 text-gray-900">
                    <div className="absolute left-0 top-0 flex h-10 w-10 items-center justify-center rounded-lg">
                      <Image
                        src={service.icon}
                        alt={service.name}
                        width={40}
                        height={40}
                        className="filter"
                      />
                    </div>
                    {service.name}
                    <span className="ml-2 text-sm font-normal text-gray-500">
                      {service.duration}
                    </span>
                  </dt>
                  <dd className="mt-2 text-base leading-7 text-gray-600">{service.description}</dd>
                </div>
              ))}
            </dl>
          </div>
        </div>
      </div>
      
    </div>
  );
}
