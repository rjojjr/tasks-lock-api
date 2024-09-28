import os


def get_host() -> str:
    try:
        return os.environ.get('TASKS_LOCK_API_HOST')
    except Exception as e:
        return 'localhost'


def get_protocol() -> str:
    try:
        return os.environ.get('TASKS_LOCK_API_PROTOCOL')
    except Exception as e:
        return 'http'


def get_port() -> int:
    try:
        return int(os.environ.get('TASKS_LOCK_API_PORT'))
    except Exception as e:
        return 8080