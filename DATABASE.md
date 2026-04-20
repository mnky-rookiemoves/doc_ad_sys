# Database Schema – VANDAM

## Tables
- users
- students
- requirements
- student_requirements
- activity_log
- password_reset_tokens

## Notes
- Passwords are stored hashed (SHA-256)
- Foreign keys enforce referential integrity
- Tokens expire after 30 minutes