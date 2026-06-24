'use client';

import { useState, useEffect, useCallback } from 'react';

const REFRESH_INTERVAL = 30_000;

function timeAgo(dateStr) {
  if (!dateStr) return '-';
  const diff = Date.now() - new Date(dateStr).getTime();
  const s = Math.floor(diff / 1000);
  if (s < 60) return `${s}d lalu`;
  const m = Math.floor(s / 60);
  if (m < 60) return `${m}m lalu`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}j lalu`;
  return `${Math.floor(h / 24)}h lalu`;
}

function StatusDot({ status, conclusion }) {
  if (status === 'in_progress' || status === 'queued') {
    return <span className="inline-block w-2.5 h-2.5 rounded-full bg-yellow-400 pulse" />;
  }
  if (conclusion === 'success') return <span className="inline-block w-2.5 h-2.5 rounded-full bg-green-400" />;
  if (conclusion === 'failure') return <span className="inline-block w-2.5 h-2.5 rounded-full bg-red-400" />;
  if (conclusion === 'cancelled') return <span className="inline-block w-2.5 h-2.5 rounded-full bg-gray-500" />;
  return <span className="inline-block w-2.5 h-2.5 rounded-full bg-gray-500" />;
}

function StatusBadge({ status, conclusion }) {
  const label = status === 'in_progress' ? 'Running' : status === 'queued' ? 'Queued' : conclusion || status;
  const cls =
    status === 'in_progress' || status === 'queued'
      ? 'bg-yellow-400/20 text-yellow-300'
      : conclusion === 'success'
      ? 'bg-green-400/20 text-green-300'
      : conclusion === 'failure'
      ? 'bg-red-400/20 text-red-300'
      : 'bg-gray-700 text-gray-400';
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full font-mono font-bold uppercase ${cls}`}>
      {label}
    </span>
  );
}

function Card({ children, className = '' }) {
  return (
    <div className={`bg-[#161B22] border border-[#30363D] rounded-xl p-4 ${className}`}>
      {children}
    </div>
  );
}

function StatCard({ icon, label, value, sub, color = 'text-white' }) {
  return (
    <Card className="flex flex-col gap-1">
      <div className="text-[#8B949E] text-xs flex items-center gap-1">{icon} {label}</div>
      <div className={`text-2xl font-bold font-mono ${color}`}>{value ?? '—'}</div>
      {sub && <div className="text-[#8B949E] text-xs">{sub}</div>}
    </Card>
  );
}

function Spinner() {
  return (
    <svg className="spin w-4 h-4 text-[#8B949E]" fill="none" viewBox="0 0 24 24">
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
    </svg>
  );
}

// ─────────────────────────────────────────────────────────────
// TABS
// ─────────────────────────────────────────────────────────────
const TABS = [
  { id: 'dashboard', icon: '🏠', label: 'Dashboard' },
  { id: 'builds',    icon: '🔨', label: 'Builds'    },
  { id: 'releases',  icon: '📦', label: 'Releases'  },
  { id: 'trigger',   icon: '⚡', label: 'Trigger'   },
];

// ─────────────────────────────────────────────────────────────
// MAIN
// ─────────────────────────────────────────────────────────────
export default function Dashboard() {
  const [tab, setTab] = useState('dashboard');
  const [builds, setBuilds]   = useState(null);
  const [releases, setReleases] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(null);

  const fetchAll = useCallback(async () => {
    try {
      const [b, r] = await Promise.all([
        fetch('/api/builds').then(r => r.json()),
        fetch('/api/releases').then(r => r.json()),
      ]);
      setBuilds(b);
      setReleases(r);
      setLastRefresh(new Date());
    } catch {}
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    fetchAll();
    const id = setInterval(fetchAll, REFRESH_INTERVAL);
    return () => clearInterval(id);
  }, [fetchAll]);

  const latestRun    = builds?.runs?.[0];
  const latestRelease = releases?.releases?.[0];
  const totalDl      = releases?.totalDownloads ?? 0;
  const repo         = builds?.repo;

  return (
    <div className="min-h-screen flex flex-col bg-[#0D1117]">
      {/* ── Header ── */}
      <header className="sticky top-0 z-30 bg-[#0D1117]/95 backdrop-blur border-b border-[#30363D] px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="text-xl">🤖</span>
          <div>
            <h1 className="text-sm font-bold leading-none">TermuxIwx</h1>
            <p className="text-[10px] text-[#8B949E] leading-none mt-0.5">Dashboard</p>
          </div>
          {latestRelease && (
            <span className="ml-1 text-[10px] bg-[#00C853]/20 text-[#00C853] px-2 py-0.5 rounded-full font-mono font-bold">
              {latestRelease.tag}
            </span>
          )}
        </div>
        <div className="flex items-center gap-2">
          {loading && <Spinner />}
          <button
            onClick={fetchAll}
            className="text-[#8B949E] text-xs border border-[#30363D] rounded-lg px-2 py-1 active:bg-[#161B22]"
          >
            ↻ Refresh
          </button>
        </div>
      </header>

      {/* ── Content ── */}
      <main className="flex-1 overflow-y-auto pb-24">
        {tab === 'dashboard' && (
          <DashboardTab latestRun={latestRun} latestRelease={latestRelease}
            totalDl={totalDl} repo={repo} builds={builds} releases={releases} lastRefresh={lastRefresh} />
        )}
        {tab === 'builds' && <BuildsTab runs={builds?.runs} />}
        {tab === 'releases' && <ReleasesTab releases={releases?.releases} totalDl={totalDl} />}
        {tab === 'trigger' && <TriggerTab onTrigger={fetchAll} />}
      </main>

      {/* ── Bottom Navigation ── */}
      <nav className="fixed bottom-0 left-0 right-0 z-30 bg-[#161B22] border-t border-[#30363D] flex">
        {TABS.map(t => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex-1 flex flex-col items-center justify-center py-2.5 gap-0.5 transition-colors
              ${tab === t.id ? 'text-[#00C853]' : 'text-[#8B949E]'}`}
          >
            <span className="text-lg leading-none">{t.icon}</span>
            <span className="text-[9px] font-semibold">{t.label}</span>
          </button>
        ))}
      </nav>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// DASHBOARD TAB
// ─────────────────────────────────────────────────────────────
function DashboardTab({ latestRun, latestRelease, totalDl, repo, builds, releases, lastRefresh }) {
  return (
    <div className="p-4 space-y-4">
      {/* Build Status Hero */}
      <Card className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-[#00C853]/5 to-transparent pointer-events-none" />
        <div className="flex items-start justify-between">
          <div>
            <p className="text-[#8B949E] text-xs mb-1">Status Build Terkini</p>
            {latestRun ? (
              <>
                <div className="flex items-center gap-2 mb-1">
                  <StatusDot status={latestRun.status} conclusion={latestRun.conclusion} />
                  <StatusBadge status={latestRun.status} conclusion={latestRun.conclusion} />
                </div>
                <p className="text-white font-semibold text-sm">#{latestRun.runNumber} — {latestRun.name}</p>
                <p className="text-[#8B949E] text-xs mt-0.5 line-clamp-1">{latestRun.commit}</p>
              </>
            ) : (
              <p className="text-[#8B949E] text-sm">Memuat...</p>
            )}
          </div>
          <a
            href={latestRun?.url || `https://github.com/Kztutorial99/TermuxIwx/actions`}
            target="_blank" rel="noopener noreferrer"
            className="text-[10px] text-[#00C853] border border-[#00C853]/30 rounded-lg px-2 py-1 shrink-0"
          >
            Lihat →
          </a>
        </div>
        {latestRun && (
          <div className="mt-3 pt-3 border-t border-[#30363D] flex items-center justify-between text-xs text-[#8B949E]">
            <span>Diperbarui {timeAgo(latestRun.updatedAt)}</span>
            <span>Branch: {latestRun.branch}</span>
          </div>
        )}
      </Card>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 gap-3">
        <StatCard icon="📦" label="Versi Terkini" value={latestRelease?.tag || '—'} color="text-[#00C853]"
          sub={latestRelease ? timeAgo(latestRelease.publishedAt) : ''} />
        <StatCard icon="⬇️" label="Total Download" value={totalDl.toLocaleString()}
          sub={`${releases?.releases?.length || 0} releases`} color="text-blue-400" />
        <StatCard icon="⭐" label="Stars" value={repo?.stars ?? '—'} sub="GitHub stars" color="text-yellow-400" />
        <StatCard icon="🍴" label="Forks" value={repo?.forks ?? '—'} sub="GitHub forks" color="text-purple-400" />
      </div>

      {/* Latest Release */}
      {latestRelease && (
        <Card>
          <p className="text-[#8B949E] text-xs mb-2">📦 Rilis Terbaru</p>
          <div className="flex items-center justify-between mb-2">
            <span className="font-bold text-white">{latestRelease.name || latestRelease.tag}</span>
            <span className="text-xs text-[#8B949E]">{latestRelease.apkSize}MB</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs text-[#8B949E]">⬇️ {latestRelease.downloads} downloads</span>
            {latestRelease.apkUrl && (
              <a href={latestRelease.apkUrl} target="_blank" rel="noopener noreferrer"
                className="bg-[#00C853] text-black text-xs font-bold px-3 py-1.5 rounded-lg active:opacity-80">
                Download APK
              </a>
            )}
          </div>
        </Card>
      )}

      {/* Recent Builds Mini */}
      <Card>
        <p className="text-[#8B949E] text-xs mb-3">🔨 Build Terakhir</p>
        <div className="space-y-2">
          {(builds?.runs || []).slice(0, 5).map(run => (
            <a key={run.id} href={run.url} target="_blank" rel="noopener noreferrer"
              className="flex items-center gap-3 py-1.5 border-b border-[#30363D] last:border-0">
              <StatusDot status={run.status} conclusion={run.conclusion} />
              <div className="flex-1 min-w-0">
                <p className="text-xs text-white truncate">#{run.runNumber} {run.name}</p>
                <p className="text-[10px] text-[#8B949E] truncate">{run.commit}</p>
              </div>
              <span className="text-[10px] text-[#8B949E] shrink-0">{timeAgo(run.updatedAt)}</span>
            </a>
          ))}
          {!builds?.runs?.length && <p className="text-[#8B949E] text-xs">Tidak ada data</p>}
        </div>
      </Card>

      {lastRefresh && (
        <p className="text-center text-[10px] text-[#8B949E]">
          Auto-refresh 30s • Terakhir: {lastRefresh.toLocaleTimeString('id')}
        </p>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// BUILDS TAB
// ─────────────────────────────────────────────────────────────
function BuildsTab({ runs }) {
  return (
    <div className="p-4 space-y-3">
      <h2 className="text-sm font-bold text-[#8B949E]">RIWAYAT BUILD</h2>
      {(runs || []).map(run => (
        <a key={run.id} href={run.url} target="_blank" rel="noopener noreferrer">
          <Card className="active:opacity-70 transition-opacity">
            <div className="flex items-start gap-3">
              <div className="mt-0.5">
                <StatusDot status={run.status} conclusion={run.conclusion} />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap mb-1">
                  <span className="text-white text-sm font-semibold">#{run.runNumber}</span>
                  <StatusBadge status={run.status} conclusion={run.conclusion} />
                </div>
                <p className="text-xs text-white truncate">{run.name}</p>
                <p className="text-[11px] text-[#8B949E] truncate mt-0.5">{run.commit}</p>
                <div className="flex items-center gap-3 mt-1.5 text-[10px] text-[#8B949E]">
                  <span>🌿 {run.branch}</span>
                  <span>🕐 {timeAgo(run.updatedAt)}</span>
                </div>
              </div>
              <span className="text-[#8B949E] text-lg">›</span>
            </div>
          </Card>
        </a>
      ))}
      {!runs?.length && (
        <Card><p className="text-[#8B949E] text-sm text-center">Belum ada build data</p></Card>
      )}
      <a href="https://github.com/Kztutorial99/TermuxIwx/actions" target="_blank" rel="noopener noreferrer"
        className="block text-center text-xs text-[#00C853] py-2">
        Lihat semua di GitHub Actions →
      </a>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// RELEASES TAB
// ─────────────────────────────────────────────────────────────
function ReleasesTab({ releases, totalDl }) {
  return (
    <div className="p-4 space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-bold text-[#8B949E]">SEMUA RILIS</h2>
        <span className="text-xs text-[#8B949E]">⬇️ {totalDl.toLocaleString()} total</span>
      </div>
      {(releases || []).map((rel, i) => (
        <Card key={rel.id}>
          <div className="flex items-start justify-between mb-2">
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <span className="text-white font-bold text-sm font-mono">{rel.tag}</span>
                {i === 0 && (
                  <span className="text-[10px] bg-[#00C853]/20 text-[#00C853] px-1.5 py-0.5 rounded-full font-bold">
                    TERBARU
                  </span>
                )}
                {rel.prerelease && (
                  <span className="text-[10px] bg-yellow-400/20 text-yellow-400 px-1.5 py-0.5 rounded-full">
                    PRE
                  </span>
                )}
              </div>
              <p className="text-[#8B949E] text-xs mt-0.5">{timeAgo(rel.publishedAt)}</p>
            </div>
            <div className="text-right">
              <p className="text-[#00C853] font-bold text-sm">{rel.downloads}</p>
              <p className="text-[10px] text-[#8B949E]">downloads</p>
            </div>
          </div>

          {/* Download bar */}
          <div className="w-full bg-[#21262D] rounded-full h-1.5 mb-3">
            <div
              className="bg-[#00C853] h-1.5 rounded-full transition-all"
              style={{ width: totalDl > 0 ? `${Math.min(100, (rel.downloads / totalDl) * 100)}%` : '0%' }}
            />
          </div>

          <div className="flex items-center gap-2">
            {rel.apkUrl && (
              <a href={rel.apkUrl} target="_blank" rel="noopener noreferrer"
                className="flex-1 bg-[#00C853] text-black text-xs font-bold text-center py-2 rounded-lg active:opacity-80">
                ⬇ Download APK {rel.apkSize ? `(${rel.apkSize}MB)` : ''}
              </a>
            )}
            <a href={rel.url} target="_blank" rel="noopener noreferrer"
              className="border border-[#30363D] text-[#8B949E] text-xs px-3 py-2 rounded-lg active:bg-[#21262D]">
              GitHub
            </a>
          </div>
        </Card>
      ))}
      {!releases?.length && (
        <Card><p className="text-[#8B949E] text-sm text-center">Belum ada rilis</p></Card>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// TRIGGER TAB
// ─────────────────────────────────────────────────────────────
function TriggerTab({ onTrigger }) {
  const [notes, setNotes]       = useState('');
  const [key, setKey]           = useState('');
  const [loading, setLoading]   = useState(false);
  const [result, setResult]     = useState(null);

  const handleTrigger = async () => {
    if (!key) { setResult({ error: 'Admin key wajib diisi' }); return; }
    setLoading(true);
    setResult(null);
    try {
      const res = await fetch('/api/trigger', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ adminKey: key, releaseNotes: notes }),
      });
      const data = await res.json();
      setResult(data);
      if (data.success) { setNotes(''); setTimeout(onTrigger, 3000); }
    } catch (e) {
      setResult({ error: e.message });
    } finally { setLoading(false); }
  };

  return (
    <div className="p-4 space-y-4">
      <h2 className="text-sm font-bold text-[#8B949E]">TRIGGER BUILD BARU</h2>

      <Card>
        <p className="text-xs text-[#8B949E] mb-4">
          Picu GitHub Actions untuk build & release APK baru secara manual.
        </p>

        <div className="space-y-3">
          <div>
            <label className="text-xs text-[#8B949E] block mb-1">Admin Key *</label>
            <input
              type="password"
              value={key}
              onChange={e => setKey(e.target.value)}
              placeholder="Masukkan admin key..."
              className="w-full bg-[#21262D] border border-[#30363D] rounded-lg px-3 py-2.5 text-sm text-white placeholder-[#8B949E] focus:outline-none focus:border-[#00C853]"
            />
          </div>

          <div>
            <label className="text-xs text-[#8B949E] block mb-1">Catatan Rilis (opsional)</label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value)}
              placeholder="Tulis perubahan di versi ini..."
              rows={4}
              className="w-full bg-[#21262D] border border-[#30363D] rounded-lg px-3 py-2.5 text-sm text-white placeholder-[#8B949E] focus:outline-none focus:border-[#00C853] resize-none"
            />
          </div>

          <button
            onClick={handleTrigger}
            disabled={loading}
            className="w-full bg-[#00C853] text-black font-bold py-3 rounded-xl text-sm disabled:opacity-50 active:opacity-80 flex items-center justify-center gap-2"
          >
            {loading ? <><Spinner /> Memicu build...</> : '⚡ Trigger Build Sekarang'}
          </button>
        </div>

        {result && (
          <div className={`mt-3 p-3 rounded-lg text-sm ${result.success ? 'bg-green-400/10 text-green-300' : 'bg-red-400/10 text-red-300'}`}>
            {result.success ? `✅ ${result.message}` : `❌ ${result.error}`}
          </div>
        )}
      </Card>

      <Card>
        <p className="text-xs text-[#8B949E] mb-2">ℹ️ Info</p>
        <ul className="space-y-1.5 text-xs text-[#8B949E]">
          <li>• Build menggunakan GitHub Actions (sekitar 5-10 menit)</li>
          <li>• APK akan otomatis di-release ke GitHub Releases</li>
          <li>• App di perangkat akan dapat notifikasi update</li>
          <li>• Admin key diatur via environment variable ADMIN_KEY</li>
        </ul>
      </Card>

      <a
        href="https://github.com/Kztutorial99/TermuxIwx/actions"
        target="_blank" rel="noopener noreferrer"
        className="block text-center bg-[#161B22] border border-[#30363D] text-[#8B949E] text-sm py-3 rounded-xl active:opacity-70"
      >
        🔨 Lihat Semua Build di GitHub →
      </a>
    </div>
  );
}
