#ifndef _CLIENT_H_
#define _CLIENT_H_

typedef void (*event_receive_callback_t)(const char *event, unsigned short len);

int connect_to_server();

void disconnect_from_server();

void send_command(const char *command);

void register_event_receive_callback(event_receive_callback_t cb);

#endif // _CLIENT_H_