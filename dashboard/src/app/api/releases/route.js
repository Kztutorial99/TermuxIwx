const OWNER = 'Kztutorial99';
const REPO  = 'TermuxIwx';
const TOKEN = process.env.GITHUB_TOKEN;

export async function GET() {
  try {
    const headers = {
      Accept: 'application/vnd.github.v3+json',
      'User-Agent': 'TermuxIwx-Dashboard',
      ...(TOKEN ? { Authorization: `token ${TOKEN}` } : {}),
    };

    const res = await fetch(
      `https://api.github.com/repos/${OWNER}/${REPO}/releases?per_page=10`,
      { headers, next: { revalidate: 60 } }
    );

    if (!res.ok) return Response.json({ releases: [], totalDownloads: 0 });

    const data = await res.json();
    let totalDownloads = 0;

    const releases = data.map(r => {
      const downloads = r.assets.reduce((sum, a) => sum + a.download_count, 0);
      totalDownloads += downloads;
      const apk = r.assets.find(a => a.name.endsWith('.apk'));
      return {
        id: r.id,
        tag: r.tag_name,
        name: r.name,
        body: r.body?.slice(0, 300) || '',
        publishedAt: r.published_at,
        url: r.html_url,
        downloads,
        apkUrl: apk?.browser_download_url || null,
        apkSize: apk ? Math.round(apk.size / 1024 / 1024 * 10) / 10 : null,
        prerelease: r.prerelease,
        draft: r.draft,
      };
    });

    return Response.json({ releases, totalDownloads });
  } catch (e) {
    return Response.json({ error: e.message, releases: [], totalDownloads: 0 }, { status: 500 });
  }
}
