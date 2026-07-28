import React from 'react';
import { CheckCircle2, Circle, AlertCircle } from 'lucide-react';

/**
 * Visual timeline indicator representing the path of a blood request.
 */
export default function RequestTimeline({ status }) {
  const steps = ['PENDING', 'VERIFIED', 'MATCHED', 'COMPLETED'];
  
  const isCancelled = status === 'CANCELLED';
  const isRejected = status === 'REJECTED';

  const getStepIndex = () => {
    if (isCancelled || isRejected) return -1;
    return steps.indexOf(status);
  };

  const currentIndex = getStepIndex();

  return (
    <div className="flex flex-col gap-6">
      <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">Request Timeline</h4>
      
      {isCancelled || isRejected ? (
        <div className="flex items-start gap-3 bg-slate-50 border border-slate-200 rounded-2xl p-4">
          <AlertCircle className={`h-5 w-5 ${isCancelled ? 'text-slate-500' : 'text-red-500'} shrink-0`} />
          <div>
            <h5 className="text-sm font-bold text-gray-800">
              Request {isCancelled ? 'Cancelled' : 'Rejected'}
            </h5>
            <p className="text-xs text-gray-400 mt-0.5">
              This request timeline has ended due to {isCancelled ? 'patient cancellation' : 'hospital rejection'}.
            </p>
          </div>
        </div>
      ) : (
        <div className="relative border-l-2 border-slate-100 ml-3 pl-6 flex flex-col gap-8">
          {steps.map((step, idx) => {
            const isActive = idx <= currentIndex;
            const isCurrent = idx === currentIndex;
            
            return (
              <div key={step} className="relative">
                <span className="absolute -left-[35px] top-0.5 flex h-6 w-6 items-center justify-center rounded-full bg-white border border-slate-200">
                  {isActive ? (
                    <CheckCircle2 className={`h-5 w-5 ${isCurrent ? 'text-primary' : 'text-green-600'} fill-white`} />
                  ) : (
                    <Circle className="h-4 w-4 text-slate-350" />
                  )}
                </span>
                
                <div>
                  <h5 className={`text-sm font-bold ${isActive ? 'text-gray-800' : 'text-gray-400'}`}>
                    {step}
                  </h5>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {step === 'PENDING' && 'Submitted. Awaiting hospital verification.'}
                    {step === 'VERIFIED' && 'Verified by hospital. Transferred to matching engine.'}
                    {step === 'MATCHED' && 'Compatibility matches located. Waiting for donor response.'}
                    {step === 'COMPLETED' && 'Donation logged and transfusion package fulfilled.'}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
