package com.my.relink.chat.service;

import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class CountingMessageRepository implements MessageRepository {

    private final MessageRepository delegate;
    private final AtomicInteger savedMessageCount;
    private final AtomicInteger failedSaveCount;

    public CountingMessageRepository(@Qualifier("messageRepository") MessageRepository delegate) {
        this.delegate = delegate;
        this.savedMessageCount = new AtomicInteger(0);
        this.failedSaveCount = new AtomicInteger(0);
    }

    @Override
    public Message save(Message message) {
        try {
            Message saved = delegate.save(message);
            savedMessageCount.incrementAndGet();
            return saved;
        } catch (Exception e) {
            failedSaveCount.incrementAndGet();
            throw e;
        }
    }

    @Override
    public void deleteMessage(Long tradeId) {
    }

    @Override
    public List<Message> findMessagesBeforeCursor(Long tradeId, Long cursor, Pageable pageable) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Message> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Message> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Message> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Message getOne(Long aLong) {
        return null;
    }

    @Override
    public Message getById(Long aLong) {
        return null;
    }

    @Override
    public Message getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Message> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Message> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Message> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public List<Message> findAll() {
        return null;
    }

    @Override
    public List<Message> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public Optional<Message> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Message entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Message> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<Message> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Message> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Message> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Message> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Message> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Message> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Message, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    public int getSavedCount() {
        return savedMessageCount.get();
    }

    public int getFailedCount(){
        return failedSaveCount.get();
    }
}
