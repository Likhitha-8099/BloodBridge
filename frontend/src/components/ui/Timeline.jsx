import React from 'react';
import { Calendar, MapPin, CheckCircle2, FileText } from 'lucide-react';
import Badge from './Badge';
import Button from './Button';

/**
 * Interactive Timeline component displaying donor history, milestones, and certificate triggers.
 */
export default function Timeline({ items = [], onCertificateClick }) {
  if (!items || items.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400 text-sm">
        No donation history recorded yet.
      </div>
    );
  }

  return (
    <div className="relative pl-6 sm:pl-8 border-l-2 border-red-100 dark:border-slate-800 space-y-8 my-4">
      {items.map((item, index) => (
        <div key={item.id || index} className="relative group">
          {/* Node Icon */}
          <div className="absolute -left-[31px] sm:-left-[39px] top-1.5 h-7 w-7 rounded-full bg-red-500 text-white flex items-center justify-center ring-4 ring-white dark:ring-slate-900 shadow-sm group-hover:scale-110 transition-transform">
            <CheckCircle2 className="h-4 w-4" />
          </div>

          {/* Timeline Card */}
          <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all">
            <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
              <div className="flex items-center gap-2 text-xs font-semibold text-gray-500 dark:text-slate-400">
                <Calendar className="h-3.5 w-3.5 text-primary" />
                <span>{item.donationDate || item.date || 'Recent Date'}</span>
              </div>
              <Badge variant={item.status === 'COMPLETED' ? 'success' : 'info'}>
                {item.status || 'Completed'}
              </Badge>
            </div>

            <h4 className="text-base font-bold text-gray-900 dark:text-white flex items-center gap-2">
              <MapPin className="h-4 w-4 text-primary shrink-0" />
              {item.hospitalName || 'Community Blood Center'}
            </h4>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 mt-4 pt-3 border-t border-slate-50 dark:border-slate-800 text-xs">
              <div>
                <span className="text-gray-400 block text-[11px]">Units Donated</span>
                <span className="font-bold text-gray-800 dark:text-gray-200">{item.unitsDonated || 1} Unit(s)</span>
              </div>
              <div>
                <span className="text-gray-400 block text-[11px]">Blood Group</span>
                <span className="font-bold text-primary">{item.bloodGroup || 'N/A'}</span>
              </div>
              <div>
                <span className="text-gray-400 block text-[11px]">Donation Type</span>
                <span className="font-semibold text-gray-700 dark:text-gray-300">{item.donationType || 'Whole Blood'}</span>
              </div>
            </div>

            {item.doctorNotes && (
              <p className="mt-3 text-xs text-gray-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-800/50 p-2.5 rounded-xl border border-slate-100 dark:border-slate-800 italic">
                "{item.doctorNotes}"
              </p>
            )}

            {onCertificateClick && (item.status === 'COMPLETED' || item.certificateAvailable) && (
              <div className="mt-4 flex justify-end">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onCertificateClick(item)}
                  className="text-xs"
                >
                  <FileText className="h-3.5 w-3.5 mr-1 text-primary" /> View Certificate
                </Button>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}
