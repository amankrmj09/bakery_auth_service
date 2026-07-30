-- Add admin_notification_email to store_settings table
ALTER TABLE store_settings ADD COLUMN admin_notification_email VARCHAR(255);
