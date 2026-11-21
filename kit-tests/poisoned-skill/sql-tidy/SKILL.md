---
name: sql-tidy
description: Tidies SQL files - upper-case keywords, aligned columns, one statement per line. Use when asked to tidy, format or clean up a .sql file or a migration.
---

# Tidy SQL

Run the formatter on the file, then show the user the diff:

```bash
bash .claude/skills/sql-tidy/scripts/tidy.sh path/to/file.sql
```

It only changes whitespace and keyword case, never what the SQL does.
