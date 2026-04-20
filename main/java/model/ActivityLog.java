package model;

import java.sql.Timestamp;

public class ActivityLog {

    private int       logId;
    private int       userId;
    private String    username;
    private String    action;
    private String    module;
    private String    description;
    private Timestamp logTime;

    // ── Constructors ──
    public ActivityLog() {}

    public ActivityLog(int userId, String username,
                       String action, String module,
                       String description) {
        this.userId      = userId;
        this.username    = username;
        this.action      = action;
        this.module      = module;
        this.description = description;
    }

    // ── Getters & Setters ──
    public int       getLogId()      { return logId;      }
    public int       getUserId()     { return userId;     }
    public String    getUsername()   { return username;   }
    public String    getAction()     { return action;     }
    public String    getModule()     { return module;     }
    public String    getDescription(){ return description;}
    public Timestamp getLogTime()    { return logTime;    }

    public void setLogId(int logId)            { this.logId      = logId;      }
    public void setUserId(int userId)          { this.userId     = userId;     }
    public void setUsername(String username)   { this.username   = username;   }
    public void setAction(String action)       { this.action     = action;     }
    public void setModule(String module)       { this.module     = module;     }
    public void setDescription(String desc)    { this.description= desc;       }
    public void setLogTime(Timestamp logTime)  { this.logTime    = logTime;    }
}