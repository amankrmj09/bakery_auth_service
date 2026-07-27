-- Add two-factor authentication and login notifications settings to users table
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN login_notifications_enabled BOOLEAN DEFAULT TRUE;
