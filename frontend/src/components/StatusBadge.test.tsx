import { render, screen } from "@testing-library/react";
import { StatusBadge } from "./StatusBadge";

describe("StatusBadge", () => {
  it("shows a friendly label", () => {
    render(<StatusBadge status="OVERDUE" />);
    const badge = screen.getByText("Overdue");
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass("badge-overdue");
  });
});
