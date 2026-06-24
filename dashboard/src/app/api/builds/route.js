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

    const [runsRes, repoRes] = await Promise.all([
      fetch(`https://api.github.com/repos/${OWNER}/${REPO}/actions/runs?per_page=10`, { headers, next: { revalidate: 30 } }),
      fetch(`https://api.github.com/repos/${OWNER}/${REPO}`, { headers, next: { revalidate: 60 } }),
    ]);

    const runsData = runsRes.ok ? await runsRes.json() : { workflow_runs: [] };
    const repoData = repoRes.ok ? await repoRes.json() : {};

    const runs = (runsData.workflow_runs || []).map(r => ({
      id: r.id,
      name: r.name,
      status: r.status,
      conclusion: r.conclusion,
      runNumber: r.run_number,
      createdAt: r.created_at,
      updatedAt: r.updated_at,
      url: r.html_url,
      branch: r.head_branch,
      commit: r.head_commit?.message?.split('\n')[0] || '',
    }));

    return Response.json({
      runs,
      repo: {
        stars: repoData.stargazers_count || 0,
        forks: repoData.forks_count || 0,
        watchers: repoData.watchers_count || 0,
      },
    });
  } catch (e) {
    return Response.json({ error: e.message, runs: [] }, { status: 500 });
  }
}
