import os


def get_host() -> str:
    host = os.environ.get('TASKS_LOCK_API_HOST')
    return host if host is not None else 'localhost'


def get_protocol() -> str:
    proto = os.environ.get('TASKS_LOCK_API_PROTOCOL')
    return proto if proto is not None else 'http'


def get_port() -> int:
    try:
        return int(os.environ.get('TASKS_LOCK_API_PORT'))
    except Exception as e:
        return 8080