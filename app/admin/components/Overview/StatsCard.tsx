'use client';

import type { ComponentType } from 'react';
import type { SVGProps } from 'react';

interface StatsCardProps {
  title: string;
  value: number;
  icon: ComponentType<SVGProps<SVGSVGElement>>;
  iconColor: string;
  linkText: string;
  linkHref: string;
}

export default function StatsCard({ title, value, icon: Icon, iconColor, linkText, linkHref }: StatsCardProps) {
  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-5">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icon className={`h-6 w-6 ${iconColor}`} />
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">{title}</dt>
              <dd className="flex items-baseline">
                <div className="text-2xl font-semibold text-gray-900">{value}</div>
              </dd>
            </dl>
          </div>
        </div>
      </div>
      <div className="bg-gray-50 px-5 py-3">
        <div className="text-sm">
          <a href={linkHref} className="font-medium text-indigo-600 hover:text-indigo-500">
            {linkText}
          </a>
        </div>
      </div>
    </div>
  );
}
