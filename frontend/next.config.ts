import type { NextConfig } from "next";

// The browser calls /api/... on the Next.js server, which passes it on to the
// Spring Boot API. That way there is no CORS setup to get wrong.
const apiUrl = process.env.API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${apiUrl}/api/:path*` }];
  },
};

export default nextConfig;
