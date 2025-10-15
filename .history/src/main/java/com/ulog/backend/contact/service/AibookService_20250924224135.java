package com.ulog.backend.contact.service;

import com.ulog.backend.contact.dto.AibookDto;

public interface AibookService {
    AibookDto generate(String contactDesc, String userDesc, String language);
}
