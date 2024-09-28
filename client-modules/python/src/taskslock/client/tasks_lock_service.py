from utils import host_utils
import requests
import json


class TasksLockService:
    # TODO - 6IA - release all locks on shutdown
    def __init__(self,
                 protocol: str | None = None,
                 host: str | None = None,
                 port: int | None = None):
        self._protocol = protocol if not protocol is None else host_utils.get_host()
        self._host = host if not host is None else host_utils.get_host()
        self._port = port if not port is None else host_utils.get_port()
        self._url = f'{self._protocol}://{self._host}:{str(self._port)}'

    def acquire_lock(self, task_name: str, context_id: str, wait_for_lock: bool):
        response = requests.get(f'{self._url}/tasks-lock/api/v1/acquire?taskName={task_name}&contextId={context_id}&waitForLock={"true" if wait_for_lock else "false"}')
        if response.status_code < 300:
            body = json.loads(response.content)
            return TaskLock(body, lambda: self.release_lock(task_name))

    def release_lock(self, task_name: str) -> bool:
        response = requests.get(f'{self._url}/tasks-lock/api/v1/release?taskName={task_name}')
        if response.status_code < 300:
            return True
        return False
