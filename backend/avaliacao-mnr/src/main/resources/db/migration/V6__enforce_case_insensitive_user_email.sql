UPDATE tb_users SET email = LOWER(TRIM(email)) WHERE email <> LOWER(TRIM(email));

CREATE UNIQUE INDEX IF NOT EXISTS ux_tb_users_email_lower ON tb_users (LOWER(email));
