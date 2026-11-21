import type { Metadata } from "next";
import { Nav } from "@/components/Nav";
import "./globals.css";

export const metadata: Metadata = {
  title: "HelloBill",
  description: "A demo billing app for learning. All data is fake.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <Nav />
        <main className="main">{children}</main>
        <footer className="footer">
          HelloBill demo app · fake data only · not for real billing
        </footer>
      </body>
    </html>
  );
}
