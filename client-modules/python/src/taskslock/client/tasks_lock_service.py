from utils import host_utils
import requests
import json
from models.task_lock import TaskLock
from logging import getLogger

version = '1.0.0'

class TasksLockService:
    """Acquires/releases locks from the TasksLockAPI."""

    # TODO - 6IA - release all locks on shutdown
    def __init__(self,
                 protocol: str | None = None,
                 host: str | None = None,
                 port: int | None = None):
        self._protocol = protocol if not protocol is None else host_utils.get_host()
        self._host = host if not host is None else host_utils.get_host()
        self._port = port if not port is None else host_utils.get_port()
        self._url = f'{self._protocol}://{self._host}:{str(self._port)}'
        self.version = version
        self.logger = getLogger(__name__)

    def acquire_lock(self, task_name: str, context_id: str, wait_for_lock: bool) -> TaskLock:
        """Acquires lock from the TasksLockAPI."""

        task_lock = TaskLock({'taskName': task_name, 'contextId': context_id, 'isLockAcquired': False}, lambda : print(f'Cannot release lock for {task_name}, lock not acquired contextId: {context_id}'))
        self.logger.debug(f"acquiring lock for task {task_name} contextId: {context_id}")
        response = requests.get(f'{self._url}/tasks-lock/api/v1/acquire?taskName={task_name}&contextId={context_id}&waitForLock={"true" if wait_for_lock else "false"}')
        if response.status_code < 300:
            body = json.loads(response.content)
            task_lock = TaskLock(body, lambda: self.release_lock(task_name))
        if task_lock.is_locked:
            self.logger.debug(f"acquired lock for task {task_name} contextId: {context_id}")
        else:
            self.logger.warning(f"did not acquire lock for task {task_name} contextId: {context_id}")

        return task_lock

    def release_lock(self, task_name: str) -> bool:
        """Releases lock from the TasksLockAPI."""
        response = requests.get(f'{self._url}/tasks-lock/api/v1/release?taskName={task_name}')
        if response.status_code < 300:
            return True
        return False
