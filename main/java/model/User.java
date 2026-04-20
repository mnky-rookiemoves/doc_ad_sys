package model;

import java.sql.Timestamp;

public class User {

    private int       userId;
    private String    username;
    private String    password;
    private String    email;
    private String    fullName;
    private String    phone;
    private String    role;
    private boolean   isActive;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // ── Constructors ──
    public User() {}

    // ── Original constructor — kept for compatibility ──
    public User(int userId, String username,
                String email, String role) {
        this.userId   = userId;
        this.username = username;
        this.email    = email;
        this.role     = role;
        this.isActive = true;
    }

    // ── Full constructor ──
    public User(int userId, String username, String password,
                String email, String fullName, String phone,
                String role, boolean isActive,
                Timestamp createdAt, Timestamp lastLogin) {
        this.userId    = userId;
        this.username  = username;
        this.password  = password;
        this.email     = email;
        this.fullName  = fullName;
        this.phone     = phone;
        this.role      = role;
        this.isActive  = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // ── Getters & Setters ──
    public int       getUserId()    { return userId;    }
    public String    getUsername()  { return username;  }
    public String    getPassword()  { return password;  }
    public String    getEmail()     { return email;     }
    public String    getFullName()  { return fullName;  }
    public String    getPhone()     { return phone;     }
    public String    getRole()      { return role;      }
    public boolean   isActive()     { return isActive;  }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getLastLogin() { return lastLogin; }

    public void setUserId(int userId)          { this.userId    = userId;    }
    public void setUsername(String username)   { this.username  = username;  }
    public void setPassword(String password)   { this.password  = password;  }
    public void setEmail(String email)         { this.email     = email;     }
    public void setFullName(String fullName)   { this.fullName  = fullName;  }
    public void setPhone(String phone)         { this.phone     = phone;     }
    public void setRole(String role)           { this.role      = role;      }
    public void setActive(boolean isActive)    { this.isActive  = isActive;  }
    public void setCreatedAt(Timestamp t)      { this.createdAt = t;         }
    public void setLastLogin(Timestamp t)      { this.lastLogin = t;         }

    // ── Role helper methods ──
    public boolean isSuperAdmin() {
        return "superadmin".equalsIgnoreCase(role);
    }
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
    public boolean isStaff() {
        return "staff".equalsIgnoreCase(role);
    }
    public boolean isAdminOrAbove() {
        return isSuperAdmin() || isAdmin();
    }
}