from typing import Callable


class TaskLock:
    def __init__(self, api_response: dict, release: Callable):
        self.task_name = api_response['taskName']
        self.context_id = api_response['contextId']
        self.is_locked = api_response['isLockAcquired']
        self.release = release
