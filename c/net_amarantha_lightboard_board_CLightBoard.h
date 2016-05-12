/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_amarantha_lightboard_board_CLightBoard */

#ifndef _Included_net_amarantha_lightboard_board_CLightBoard
#define _Included_net_amarantha_lightboard_board_CLightBoard
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    init
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_net_amarantha_lightboard_board_CLightBoard_init
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    update
 * Signature: ([[[D)V
 */
JNIEXPORT void JNICALL Java_net_amarantha_lightboard_board_CLightBoard_update
  (JNIEnv *, jobject, jintArray);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    getUpdateInterval
 * Signature: ()Ljava/lang/Long;
 */
JNIEXPORT jobject JNICALL Java_net_amarantha_lightboard_board_CLightBoard_getUpdateInterval
  (JNIEnv *, jobject);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    getRows
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_amarantha_lightboard_board_CLightBoard_getRows
  (JNIEnv *, jobject);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    getCols
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_amarantha_lightboard_board_CLightBoard_getCols
  (JNIEnv *, jobject);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    sleep
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_amarantha_lightboard_board_CLightBoard_sleep
  (JNIEnv *, jobject);

/*
 * Class:     net_amarantha_lightboard_board_CLightBoard
 * Method:    wake
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_amarantha_lightboard_board_CLightBoard_wake
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
