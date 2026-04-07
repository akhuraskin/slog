// Copyright 2022 Woven Planet Holdings
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// NOLINT(namespace-avsoftware)
#include <jni.h>

#include <cstdint>
#include <memory>
#include <string>
#include <vector>

#include "slog_cc/buffer/buffer.h"
#include "slog_cc/context/context.h"
#include "slog_cc/events/event.h"
#include "slog_cc/events/scope.h"
#include "slog_cc/primitives/call_site.h"
#include "slog_cc/primitives/record.h"
#include "slog_cc/primitives/tag.h"
#include "slog_cc/printer/printer.h"

// Cached class and method IDs for bufferFlush performance.
static jclass g_tagClass = nullptr;
static jmethodID g_tagCtor = nullptr;
static jclass g_recordClass = nullptr;
static jmethodID g_recordCtor = nullptr;
static jclass g_callSiteClass = nullptr;
static jmethodID g_callSiteCtor = nullptr;
static jclass g_bufferDataClass = nullptr;
static jmethodID g_bufferDataCtor = nullptr;

static std::string jstringToString(JNIEnv* env, jstring jstr) {
  if (jstr == nullptr) return "";
  const char* chars = env->GetStringUTFChars(jstr, nullptr);
  std::string result(chars);
  env->ReleaseStringUTFChars(jstr, chars);
  return result;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*) {
  JNIEnv* env;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK) {
    return JNI_ERR;
  }

  jclass cls;

  cls = env->FindClass("com/woven/slog/SlogTag");
  g_tagClass = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
  g_tagCtor = env->GetMethodID(g_tagClass, "<init>",
                               "(Ljava/lang/String;Ljava/lang/String;JDI)V");

  cls = env->FindClass("com/woven/slog/SlogRecord");
  g_recordClass = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
  g_recordCtor = env->GetMethodID(
      g_recordClass, "<init>",
      "(IIBJJ[Lcom/woven/slog/SlogTag;Ljava/lang/String;Ljava/lang/String;"
      "Ljava/lang/String;)V");

  cls = env->FindClass("com/woven/slog/SlogCallSite");
  g_callSiteClass = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
  g_callSiteCtor = env->GetMethodID(g_callSiteClass, "<init>",
                                    "(Ljava/lang/String;Ljava/lang/String;I)V");

  cls = env->FindClass("com/woven/slog/SlogBufferData");
  g_bufferDataClass = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
  g_bufferDataCtor = env->GetMethodID(
      g_bufferDataClass, "<init>",
      "([Lcom/woven/slog/SlogRecord;[Lcom/woven/slog/SlogCallSite;)V");

  return JNI_VERSION_1_8;
}

// --- SlogContext ---

extern "C" JNIEXPORT jlong JNICALL
Java_com_woven_slog_SlogNative_contextGetInstance(JNIEnv*, jclass) {
  auto* ptr =
      new std::shared_ptr<slog::SlogContext>(slog::SlogContext::getInstance());
  return reinterpret_cast<jlong>(ptr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_contextRelease(JNIEnv*, jclass,
                                              jlong contextPtr) {
  auto* ptr =
      reinterpret_cast<std::shared_ptr<slog::SlogContext>*>(contextPtr);
  delete ptr;
}

// --- Call site management ---

extern "C" JNIEXPORT jint JNICALL
Java_com_woven_slog_SlogNative_addOrReuseCallSiteVerySlow(JNIEnv* env, jclass,
                                                          jstring function,
                                                          jstring file,
                                                          jint line) {
  return slog::SlogContext::getInstance()->addOrReuseCallSiteVerySlow(
      jstringToString(env, function), jstringToString(env, file),
      static_cast<int32_t>(line));
}

// --- SlogEvent lifecycle ---

extern "C" JNIEXPORT jlong JNICALL
Java_com_woven_slog_SlogNative_eventCreate(JNIEnv*, jclass, jbyte severity,
                                           jint callSiteId) {
  auto* event = new slog::SlogEvent(static_cast<int8_t>(severity),
                                    static_cast<int32_t>(callSiteId));
  return reinterpret_cast<jlong>(event);
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_eventAddTagString(JNIEnv* env, jclass,
                                                 jlong eventPtr, jstring key,
                                                 jstring value) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  event->addTag(jstringToString(env, key), jstringToString(env, value));
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_eventAddTagLong(JNIEnv* env, jclass,
                                               jlong eventPtr, jstring key,
                                               jlong value) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  event->addTag(jstringToString(env, key), static_cast<int64_t>(value));
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_eventAddTagDouble(JNIEnv* env, jclass,
                                                 jlong eventPtr, jstring key,
                                                 jdouble value) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  event->addTag(jstringToString(env, key), static_cast<double>(value));
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_eventEmitValue(JNIEnv* env, jclass,
                                              jlong eventPtr, jstring value) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  *event << jstringToString(env, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_eventDestroy(JNIEnv*, jclass, jlong eventPtr) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  delete event;
}

// --- SlogScope lifecycle ---

extern "C" JNIEXPORT jlong JNICALL
Java_com_woven_slog_SlogNative_scopeCreate(JNIEnv*, jclass, jlong eventPtr) {
  auto* event = reinterpret_cast<slog::SlogEvent*>(eventPtr);
  auto* scope = new slog::SlogScope(*event);
  return reinterpret_cast<jlong>(scope);
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_scopeDestroy(JNIEnv*, jclass, jlong scopePtr) {
  auto* scope = reinterpret_cast<slog::SlogScope*>(scopePtr);
  delete scope;
}

// --- SlogBuffer lifecycle ---

extern "C" JNIEXPORT jlong JNICALL
Java_com_woven_slog_SlogNative_bufferCreate(JNIEnv*, jclass,
                                            jlong contextPtr) {
  auto* ctxPtr =
      reinterpret_cast<std::shared_ptr<slog::SlogContext>*>(contextPtr);
  auto* buffer = new slog::SlogBuffer(*ctxPtr);
  return reinterpret_cast<jlong>(buffer);
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_bufferDestroy(JNIEnv*, jclass,
                                             jlong bufferPtr) {
  auto* buffer = reinterpret_cast<slog::SlogBuffer*>(bufferPtr);
  delete buffer;
}

extern "C" JNIEXPORT void JNICALL
Java_com_woven_slog_SlogNative_bufferWaitSlogQueue(JNIEnv*, jclass,
                                                   jlong bufferPtr) {
  auto* buffer = reinterpret_cast<slog::SlogBuffer*>(bufferPtr);
  buffer->waitSlogQueue();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_woven_slog_SlogNative_bufferFlush(JNIEnv* env, jclass,
                                           jlong bufferPtr) {
  auto* buffer = reinterpret_cast<slog::SlogBuffer*>(bufferPtr);
  slog::SlogBufferData data = buffer->flush();
  slog::SlogPrinter printer;

  // Convert records.
  jobjectArray jRecords =
      env->NewObjectArray(data.records.size(), g_recordClass, nullptr);
  for (size_t i = 0; i < data.records.size(); ++i) {
    const auto& record = data.records[i];

    // Convert tags for this record.
    jobjectArray jTags =
        env->NewObjectArray(record.tags().size(), g_tagClass, nullptr);
    for (size_t j = 0; j < record.tags().size(); ++j) {
      const auto& tag = record.tags()[j];
      jstring jKey = env->NewStringUTF(tag.key().c_str());
      jstring jValueString = env->NewStringUTF(tag.valueString().c_str());
      jobject jTag =
          env->NewObject(g_tagClass, g_tagCtor, jKey, jValueString,
                         static_cast<jlong>(tag.valueInt()),
                         static_cast<jdouble>(tag.valueDouble()),
                         static_cast<jint>(tag.valueType()));
      env->SetObjectArrayElement(jTags, j, jTag);
      env->DeleteLocalRef(jTag);
      env->DeleteLocalRef(jKey);
      env->DeleteLocalRef(jValueString);
    }

    std::string jsonStr = printer.jsonString(record);
    std::string slogTextStr = printer.slogText(record);
    std::string flatTextStr = printer.flatText(record);
    jstring jJsonStr = env->NewStringUTF(jsonStr.c_str());
    jstring jSlogText = env->NewStringUTF(slogTextStr.c_str());
    jstring jFlatText = env->NewStringUTF(flatTextStr.c_str());

    jobject jRecord = env->NewObject(
        g_recordClass, g_recordCtor,
        static_cast<jint>(record.thread_id()),
        static_cast<jint>(record.call_site_id()),
        static_cast<jbyte>(record.severity()),
        static_cast<jlong>(record.time().elapsed_ns),
        static_cast<jlong>(record.time().global_ns), jTags, jJsonStr,
        jSlogText, jFlatText);
    env->SetObjectArrayElement(jRecords, i, jRecord);
    env->DeleteLocalRef(jRecord);
    env->DeleteLocalRef(jTags);
    env->DeleteLocalRef(jJsonStr);
    env->DeleteLocalRef(jSlogText);
    env->DeleteLocalRef(jFlatText);
  }

  // Convert call sites.
  jobjectArray jCallSites =
      env->NewObjectArray(data.call_sites.size(), g_callSiteClass, nullptr);
  for (size_t i = 0; i < data.call_sites.size(); ++i) {
    const auto& cs = data.call_sites[i];
    jstring jFunction = env->NewStringUTF(cs.function().c_str());
    jstring jFile = env->NewStringUTF(cs.file().c_str());
    jobject jCallSite = env->NewObject(g_callSiteClass, g_callSiteCtor,
                                       jFunction, jFile,
                                       static_cast<jint>(cs.line()));
    env->SetObjectArrayElement(jCallSites, i, jCallSite);
    env->DeleteLocalRef(jCallSite);
    env->DeleteLocalRef(jFunction);
    env->DeleteLocalRef(jFile);
  }

  // Create SlogBufferData.
  jobject result = env->NewObject(g_bufferDataClass, g_bufferDataCtor,
                                  jRecords, jCallSites);
  env->DeleteLocalRef(jRecords);
  env->DeleteLocalRef(jCallSites);
  return result;
}
