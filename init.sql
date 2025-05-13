create table task_locks (
    task_name VARCHAR(256) PRIMARY KEY,
    is_locked BIT NOT NULL,
    is_locked_by_host VARCHAR(256),
    context_id VARCHAR(256),
    locked_at DATETIME
);