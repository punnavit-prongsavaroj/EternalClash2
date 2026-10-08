import psycopg2

try:
    conn = psycopg2.connect("postgres://postgres.llyeafyezqnrllgqfkyt:pGUFO7nirtee0vbm@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres")
    cur = conn.cursor()
    cur.execute("ALTER TABLE cities ADD COLUMN IF NOT EXISTS game_id BIGINT;")
    conn.commit()
    print("Added game_id")
except Exception as e:
    print(e)
