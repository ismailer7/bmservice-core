package com.bmservice.core.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadException implements Thread.UncaughtExceptionHandler
{
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        log.error(e.getMessage());
    }
}

