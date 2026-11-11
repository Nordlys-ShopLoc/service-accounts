-- US-S16: a client registered in a shop may have no e-mail.
-- PostgreSQL accepts several NULL values in a UNIQUE column,
-- so filled-in e-mails stay unique.
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
