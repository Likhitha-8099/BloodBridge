import React from 'react';
import { Award, Shield, Heart, Zap, Sparkles, Star, Trophy } from 'lucide-react';
import Badge from '../ui/Badge';
import Card from '../ui/Card';

/**
 * Achievement System displaying donor level badges, milestones, and tier unlocks.
 */
export default function AchievementCard({ donorScore = 100, totalDonations = 0, livesSaved = 0 }) {
  // Compute Tier Level based on totalDonations / score
  const getTier = () => {
    if (totalDonations >= 10 || donorScore >= 200) return { name: 'Life Saver Hero', variant: 'hero', icon: Trophy, progress: 100 };
    if (totalDonations >= 5 || donorScore >= 160) return { name: 'Platinum Donor', variant: 'platinum', icon: Sparkles, progress: 80 };
    if (totalDonations >= 3 || donorScore >= 130) return { name: 'Gold Donor', variant: 'gold', icon: Star, progress: 60 };
    if (totalDonations >= 1 || donorScore >= 100) return { name: 'Silver Donor', variant: 'silver', icon: Award, progress: 40 };
    return { name: 'Bronze Champion', variant: 'bronze', icon: Shield, progress: 20 };
  };

  const currentTier = getTier();
  const IconComponent = currentTier.icon;

  const milestones = [
    { title: 'First Drop', desc: 'Completed 1st donation', icon: Heart, unlocked: totalDonations >= 1 },
    { title: 'Community Hero', desc: 'Saved 5+ Lives', icon: Zap, unlocked: livesSaved >= 5 },
    { title: 'Emergency Responder', desc: 'Available for urgent calls', icon: Shield, unlocked: true },
    { title: 'Century Score', desc: 'Reached 100 Donor Score', icon: Star, unlocked: donorScore >= 100 },
  ];

  return (
    <Card className="p-6 flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="font-bold text-base text-gray-900 dark:text-white flex items-center gap-2">
            <Trophy className="h-5 w-5 text-amber-500" /> Donor Level & Achievements
          </h3>
          <p className="text-xs text-gray-400 mt-0.5">Your lifetime impact and badge rank</p>
        </div>
        <Badge variant={currentTier.variant} size="lg" icon={IconComponent}>
          {currentTier.name}
        </Badge>
      </div>

      {/* Progress Bar towards Next Level */}
      <div className="flex flex-col gap-2 bg-slate-50 dark:bg-slate-800/60 p-4 rounded-2xl border border-slate-100 dark:border-slate-800">
        <div className="flex items-center justify-between text-xs font-semibold text-gray-700 dark:text-gray-300">
          <span>Current Score: <strong className="text-primary">{donorScore} pts</strong></span>
          <span>Rank Progress</span>
        </div>
        <div className="w-full bg-slate-200 dark:bg-slate-700 h-2.5 rounded-full overflow-hidden">
          <div 
            className="bg-gradient-to-r from-red-500 to-amber-500 h-2.5 rounded-full transition-all duration-500"
            style={{ width: `${currentTier.progress}%` }}
          />
        </div>
      </div>

      {/* Badges Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {milestones.map((m, i) => {
          const MIcon = m.icon;
          return (
            <div 
              key={i}
              className={`p-3 rounded-2xl border text-center flex flex-col items-center gap-1.5 transition-all ${
                m.unlocked 
                  ? 'bg-white dark:bg-slate-900 border-amber-200/80 dark:border-amber-900/30 shadow-sm' 
                  : 'bg-slate-50/50 dark:bg-slate-800/30 border-slate-100 dark:border-slate-800 opacity-50 grayscale'
              }`}
            >
              <div className={`h-9 w-9 rounded-xl flex items-center justify-center ${
                m.unlocked ? 'bg-amber-50 text-amber-600 dark:bg-amber-950/60 dark:text-amber-400' : 'bg-slate-100 text-slate-400'
              }`}>
                <MIcon className="h-5 w-5" />
              </div>
              <span className="font-bold text-xs text-gray-800 dark:text-gray-200 line-clamp-1">{m.title}</span>
              <span className="text-[10px] text-gray-400 line-clamp-1">{m.desc}</span>
            </div>
          );
        })}
      </div>
    </Card>
  );
}
