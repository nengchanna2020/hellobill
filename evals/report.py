#!/usr/bin/env python3
# Reads a run's transcript (claude -p --output-format stream-json) and writes one results.csv row:
#   report.py row RUN.jsonl TASK PASSED SECONDS TEST_LINES
# Or turns results.csv into a Markdown table with totals:
#   report.py table results.csv
import csv, json, sys

def row(path, task, passed, secs, tests):
    first = cost = turns = None; skills, agents = [], []
    for line in open(path):
        try: d = json.loads(line)
        except ValueError: continue
        if d.get('type') == 'assistant':
            u = d['message'].get('usage') or {}
            if first is None:
                first = u.get('input_tokens', 0) + u.get('cache_read_input_tokens', 0) + u.get('cache_creation_input_tokens', 0)
            for c in d['message'].get('content', []):
                if c.get('type') == 'tool_use' and c['name'] == 'Skill': skills.append(c['input'].get('skill', '?'))
                if c.get('type') == 'tool_use' and c['name'] in ('Agent', 'Task'): agents.append(c['input'].get('subagent_type', '?'))
        if d.get('type') == 'result':
            cost, turns = d.get('total_cost_usd'), d.get('num_turns')
    w = csv.writer(sys.stdout, lineterminator='\n')
    w.writerow([task, passed, secs, f'{cost or 0:.4f}', turns or 0, first or 0, ' '.join(skills) or 'none', ' '.join(agents) or 'none', tests.strip()])

def table(path):
    rows = list(csv.DictReader(open(path)))
    print('| Task | Passed | Time | Cost | First call | Skills loaded | Agents | Test lines |')
    print('|---|---|---|---|---|---|---|---|')
    for r in rows:
        print(f"| {r['task']} | {r['passed']} | {r['seconds']} s | ${float(r['cost_usd']):.2f} | {int(r['first_call_tokens']):,} | {r['skills']} | {r['agents']} | {r['test_lines']} |")
    n = len(rows); p = sum(r['passed'] == 'yes' for r in rows)
    print(f"| **{p} of {n} passed** | | {sum(int(r['seconds']) for r in rows)} s | ${sum(float(r['cost_usd']) for r in rows):.2f} | | | | |")

if __name__ == '__main__':
    {'row': lambda: row(*sys.argv[2:7]), 'table': lambda: table(sys.argv[2])}[sys.argv[1]]()
