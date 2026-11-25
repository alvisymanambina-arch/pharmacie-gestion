#!/usr/bin/env python3
"""Apply the project's PostgreSQL schema using credentials from the ignored .env file."""

from __future__ import annotations

import os
import shlex
import subprocess
import sys
from pathlib import Path
from urllib.parse import parse_qs, urlsplit


ROOT = Path(__file__).resolve().parent.parent


def read_env(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("export "):
            line = line[7:].strip()
        key, separator, value = line.partition("=")
        if not separator:
            continue
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = shlex.split(value)[0]
        values[key.strip()] = value
    return values


def main() -> int:
    env_path = ROOT / ".env"
    if not env_path.is_file():
        print("Tsy hita ny .env ao amin'ny lahatahiry fototry ny projet.", file=sys.stderr)
        return 2

    config = read_env(env_path)
    jdbc_url = config.get("PHARMACIE_DB_URL", "")
    username = config.get("PHARMACIE_DB_USER", "")
    password = config.get("PHARMACIE_DB_PASSWORD", "")
    if not jdbc_url.startswith("jdbc:postgresql://"):
        print("PHARMACIE_DB_URL dia tsy JDBC PostgreSQL URL.", file=sys.stderr)
        return 2
    if not username or not password or "REMPLACER" in password or "PASSWORD" in password:
        print("Fenoy ao amin'ny .env ny PHARMACIE_DB_USER sy PHARMACIE_DB_PASSWORD.", file=sys.stderr)
        return 2

    parsed = urlsplit(jdbc_url.removeprefix("jdbc:"))
    if not parsed.hostname:
        print("Tsy marina ny host ao amin'ny PHARMACIE_DB_URL.", file=sys.stderr)
        return 2
    database = parsed.path.lstrip("/") or "postgres"
    sslmode = parse_qs(parsed.query).get("sslmode", ["require"])[0]
    sql_file = ROOT / "src/main/resources/sql/supabase_schema.sql"
    command = [
        "psql", "--host", parsed.hostname, "--port", str(parsed.port or 5432),
        "--username", username, "--dbname", database, "--no-password",
        "--set=ON_ERROR_STOP=1", "--file", str(sql_file),
    ]
    process_env = os.environ.copy()
    process_env["PGPASSWORD"] = password
    process_env["PGSSLMODE"] = sslmode
    print(f"Mampihatra ny schema amin'ny {parsed.hostname}/{database}...")
    try:
        return subprocess.run(command, env=process_env, check=False).returncode
    except FileNotFoundError:
        print("Tsy hita ny psql. Apetraho ny postgresql-client.", file=sys.stderr)
        return 127


if __name__ == "__main__":
    raise SystemExit(main())
