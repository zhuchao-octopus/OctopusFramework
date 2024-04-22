/////////////////////////////////////////////////////////////////////////////////////////////
/*
 *  ann-dataset.h
 *  Home Page :http://www.1234998.top
 *  Created on: May 20, 2023
 *  Author: M
 */
/////////////////////////////////////////////////////////////////////////////////////////////

#ifndef _INC_ANN_CIFAR_H_
#define _INC_ANN_CIFAR_H_

#include <stdarg.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include "ann-cnn.h"

//cifar-10 /cifar-100
#define CIFAR_TRAINNING_IMAGE_COUNT 50000
#define CIFAR_TESTING_IMAGE_COUNT 10000
#define CIFAR_TRAINNING_IMAGE_SAVINT_COUNT 1000

#define CIFAR_IMAGE_WIDTH 32
#define CIFAR_IMAGE_HEIGHT 32
#define CIFAR10_TRAINNING_IMAGE_BATCH_COUNT 10000
//#define CIFAR100_IMAGE_LABEL_NUM 100
#define CIFAR10_IMAGE_SIZE (3072 + 1) // 32X32X3+1
#define CIFAR100_IMAGE_SIZE (3072 + 2) // 32X32X3+2

// �ļ���Ϣͷ�ṹ��
typedef struct BITMAPFILEHEADER {
    uint16_t bfType;        // 19778��������BM�ַ�������Ӧ��ʮ������Ϊ0x4d42��ʮ����Ϊ19778��������BMP��ʽ�ļ�
    uint32_t bfSize;        // �ļ���С�����ֽ�Ϊ��λ��2-5�ֽڣ�
    uint16_t bfReserved1;   // ��������������Ϊ0��6-7�ֽڣ�
    uint16_t bfReserved2;   // ��������������Ϊ0��8-9�ֽڣ�
    uint32_t bfOffBits;     // ���ļ�ͷ���������ݵ�ƫ�ƣ�10-13�ֽڣ�
} TBITMAPFILEHEADER;

// ͼ����Ϣͷ�ṹ��
typedef struct BITMAPINFOHEADER {
    uint32_t biSize;          // �˽ṹ��Ĵ�С��14-17�ֽڣ�
    uint32_t biWidth;         // ͼ��Ŀ�18-21�ֽڣ�
    uint32_t biHeight;        // ͼ��ĸߣ�22-25�ֽڣ�
    uint16_t biPlanes;        // ��ʾBMPͼƬ��ƽ��������Ȼ��ʾ��ֻ��һ��ƽ�棬���Ժ����1��26-27�ֽڣ�
    uint16_t biBitCount;      // һ������ռ��λ����һ��Ϊ24��28-29�ֽڣ�
    uint32_t biCompression;   // ˵��ͼ������ѹ�������ͣ�0Ϊ��ѹ����30-33�ֽڣ�
    uint32_t biSizeImage;     // ����������ռ��С�����ֵӦ�õ��������ļ�ͷ�ṹ��bfSize - bfOffBits��34-37�ֽڣ�
    uint32_t biXPelsPerMeter; // ˵��ˮƽ�ֱ��ʣ�������/�ױ�ʾ��һ��Ϊ0��38-41�ֽڣ�
    uint32_t biYPelsPerMeter; // ˵����ֱ�ֱ��ʣ�������/�ױ�ʾ��һ��Ϊ0��42-45�ֽڣ�
    uint32_t biClrUsed;       // ˵��λͼʵ��ʹ�õĲ�ɫ���е���ɫ����������Ϊ0�Ļ�����˵��ʹ�����е�ɫ�����46-49�ֽڣ�
    uint32_t biClrImportant;  // ˵����ͼ����ʾ����ҪӰ�����ɫ��������Ŀ�������0����ʾ����Ҫ��50-53�ֽڣ�
} TBITMAPINFOHEADER;

// 24λͼ������Ϣ�ṹ�壬����ɫ��
typedef struct PixelInfo {
    uint8_t rgbBlue;    // ����ɫ����ɫ������ֵ��ΧΪ0-255��
    uint8_t rgbGreen;   // ����ɫ����ɫ������ֵ��ΧΪ0-255��
    uint8_t rgbRed;     // ����ɫ�ĺ�ɫ������ֵ��ΧΪ0-255��
    uint8_t rgbReserved; // ����������Ϊ0
} TPixelInfo;


typedef enum DataSetType {
    Cifar10,
    Cifar100
} TDataSetType;

typedef struct ANN_CNN_DataSet_Image {
    TDataSetType data_type;
    uint16_t labelIndex;
    uint16_t detailIndex;
    TPVolume volume;
} TDSImage, *TPPicture;

char *GetDataSetName(uint16_t DsType);

TPPicture Dataset_GetTestingPic(uint32_t TestingIndex, uint16_t DataSetType);

TPPicture Dataset_GetTrainningPic(uint32_t TrainningIndex, uint16_t DataSetType);

TPPicture Dataset_GetPic(FILE *PFile, uint32_t ImageIndex, uint16_t DataSetType);

uint32_t CifarReadImage(const char *FileName, uint8_t *Buffer, uint32_t ImageIndex);

uint32_t Cifar10ReadImage(FILE *PFile, uint8_t *Buffer, uint32_t ImageIndex);

uint32_t Cifar100ReadImage(FILE *PFile, uint8_t *Buffer, uint32_t ImageIndex);

uint32_t ReadFileToBuffer(const char *FileName, uint8_t *Buffer, uint32_t ReadSize, uint32_t OffSet);

uint32_t ReadFileToBuffer2(FILE *PFile, uint8_t *Buffer, uint32_t ReadSize, uint32_t OffSet);

void PrintBMP(char *BMPFileName);

FILE *CreateBMP(char *BMPFileName, uint16_t biBitCount, uint16_t width, uint16_t height);

TPVolume LoadBmpFileToVolume(const char *FileName);

void SaveVolumeToBMP(TPVolume PVolume, bool_t Grads, uint16_t Depth, uint16_t biBitCount, const char *FileName);

void CloseTestingDataset();

void CloseTrainningDataset();

#endif /* _INC_ANN_CNN_H_ */