#include "client.h"
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <pthread.h>
#include <signal.h>
#include <unistd.h>
//#include <cutils/sockets.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <stdio.h>
#include <termios.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>

#include<android/log.h>

#define MAX_LINE_LEN 1024
#define TL_CONFIG_FILE_PATH "/etc/bluesoleil/TL.INI"
#define SERVER_PATH "/data/bluesoleil/virtual_tty"
//#define SERVER_SOCKET "blueletd"

static int s_serial_id;
static int s_client_id;
static int s_running;

static void *receive_message_proc(void *arg);

static event_receive_callback_t s_event_callback = 0;


#define LOG_TAG "IVTSV"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)


static int get_private_profile_string(const char *sec, const char *key, const char *defval, char *val, unsigned int size, const char *name) {
    FILE *file;
    char *line;
    int line_len;
    int isfound;
    char *str;
    int ret;
    isfound = 0;

    if (!(file = fopen(name, "r"))) {
        line = NULL;
        goto defret;
    }

    line_len = MAX_LINE_LEN + strlen(key) + 2;   /*[Key name]=[Key Value]*/
    line = (char *) malloc(line_len);

    if (line == NULL) {
        goto defret;
    }

    memset(line, 0, line_len);

    while (fgets(line, line_len, file)) {
        if (!(ret = strlen(line)) || line[0] != '[' || line[ret - 2] != ']') {
            continue;
        }

        chksec:
        line[ret - 2] = '\000';

        if (strcmp(line + 1, sec)) {
            continue;
        }

        while (fgets(line, line_len, file)) {
            if ((ret = strlen(line)) && line[0] == '['
                && line[ret - 2] == ']') {
                goto chksec;
            }

            if (!(str = strtok(line, "=\n")) || (strcmp(str, key))) {
                continue;
            }

            if (NULL != (str = strtok(NULL, "=\n"))) {
                strncpy(val, str, size);
                isfound = 1;
                break;
            }

            memset(line, 0, line_len);
        }

        break;
    }

    defret:

    if (!isfound) {
        memset(val, 0, size);

        if (defval != NULL) {
            ret = (strlen(defval) + 1) > size ? size - 1 : strlen(defval);
            memcpy(val, defval, ret);
        }
    }

    if (file) {
        fclose(file);
    }

    if (line != NULL) {
        free(line);
    }

    return strlen(val);
}

int connect_to_server() {
    int sd;
#ifdef SERVER_PSEUDO_TTY
    struct termios tty;
    char uartval[50] = {0};

    get_private_profile_string("uartsetting", "port", " ", uartval, 50, TL_CONFIG_FILE_PATH);
    LOGD("Try to open port: %s.\n", uartval);
    sd = open(uartval, O_RDWR | O_NOCTTY);

    if (sd <= 0)
    {
        LOGD("failed to open %s:%s.\n", uartval, strerror(errno));
        return -1;
    }

    /* Set raw attributes on the pty. */
    tcgetattr(sd, &tty);
    cfmakeraw(&tty);
    tcsetattr(sd, TCSAFLUSH, &tty);
#else
#ifndef SERVER_SOCKET
    struct sockaddr_un addr;

    memset(&addr, 0, sizeof(struct sockaddr_un));
    addr.sun_family = AF_UNIX;

    if (strlen(SERVER_PATH) < sizeof(addr.sun_path)) {
        strncpy(addr.sun_path, SERVER_PATH, sizeof(addr.sun_path) - 1);
    } else {
        return -1;
    }
    s_serial_id = sd;
    sd = socket(AF_UNIX, SOCK_STREAM, 0);

    if (sd == -1) {
        return -1;
    }

    if (connect(sd, (struct sockaddr *) &addr, sizeof(struct sockaddr_un)) == -1) {
        close(sd);
        return -1;
    }
#else

    if ((sd=socket_local_client(SERVER_SOCKET, ANDROID_SOCKET_NAMESPACE_RESERVED, SOCK_STREAM)) < 0)
    {
        close(sd);
        return -1;
    }
#endif
#endif
    s_client_id = sd;
    s_running = 1;

    return sd;
}

void disconnect_from_server() {
    s_running = 0;
    close(s_serial_id);
    close(s_client_id);
    s_client_id = 0;
    s_serial_id = 0;
}

void send_command(const char *command) {
    if (s_client_id > 0) {
        LOGD("\n[Send_Command] >> %s\n", command);
        write(s_client_id, command, strlen(command));
    }
}

void register_event_receive_callback(event_receive_callback_t cb) {
    s_event_callback = cb;
}
/*
static void *receive_message_proc(void *arg)
{
    int server = (int) arg;
    unsigned char event[256];
    int rc;

    if (server <= 0)
    {
        return 0;
    }

    while (s_running)
    {
        memset(event, 0, sizeof(event));
        rc = read(server, event, 256);
        
        if (rc > 0 && s_event_callback)
        {
            (*s_event_callback)((const char *) event, rc);
        }

    }
    return 0;
}
*/