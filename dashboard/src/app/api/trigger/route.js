const OWNER     = 'Kztutorial99';
const REPO      = 'TermuxIwx';
const TOKEN     = process.env.GITHUB_TOKEN;
const ADMIN_KEY = process.env.ADMIN_KEY || 'termuxiwx2024';

export async function POST(req) {
  try {
    const { adminKey, releaseNotes } = await req.json();

    if (adminKey !== ADMIN_KEY) {
      return Response.json({ error: 'Key salah' }, { status: 401 });
    }

    if (!TOKEN) {
      return Response.json({ error: 'GitHub token tidak dikonfigurasi' }, { status: 500 });
    }

    const res = await fetch(
      `https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/build-release.yml/dispatches`,
      {
        method: 'POST',
        headers: {
          Authorization: `token ${TOKEN}`,
          Accept: 'application/vnd.github.v3+json',
          'Content-Type': 'application/json',
          'User-Agent': 'TermuxIwx-Dashboard',
        },
        body: JSON.stringify({
          ref: 'main',
          inputs: { release_notes: releaseNotes || 'Triggered from TermuxIwx Dashboard' },
        }),
      }
    );

    if (res.status === 204) {
      return Response.json({ success: true, message: 'Build berhasil dipicu! Cek status di panel Builds.' });
    }

    const data = await res.json();
    return Response.json({ error: data.message || 'Gagal memicu build' }, { status: res.status });
  } catch (e) {
    return Response.json({ error: e.message }, { status: 500 });
  }
}
