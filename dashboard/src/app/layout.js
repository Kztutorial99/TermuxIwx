import './globals.css';

export const metadata = {
  title: 'TermuxIwx Dashboard',
  description: 'Monitor build, release, dan traffic TermuxIwx',
  viewport: 'width=device-width, initial-scale=1, maximum-scale=1',
  themeColor: '#0D1117',
};

export default function RootLayout({ children }) {
  return (
    <html lang="id">
      <head>
        <meta name="mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>🤖</text></svg>" />
      </head>
      <body>{children}</body>
    </html>
  );
}
